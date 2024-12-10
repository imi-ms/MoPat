package de.imi.mopat.helper.controller;

import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Optional;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GitRepositoryMetadataHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitRepositoryMetadataHandler.class);

    private static final String BASE_RELEASE_URL = "https://github.com/imi-ms/MoPat/releases/";

    private static final String TAG_PATH = "tag/";
    private static final String COMMIT_PATH = "commit/";
    private static final String LATEST_PATH = "latest/";

    private static final String GIT_SSH_PREFIX = "git@github.com:";
    private static final String GIT_HTTPS_PREFIX = "https://github.com/";

    private static final String VERSION_PREFIX = "v";

    @Value("${git.build.version:Unknown}")
    private String buildVersion;

    @Value("${git.branch:Unknown}")
    private String branch;

    @Value("${git.commit.id:Unknown}")
    private String commitId;

    @Value("${git.commit.id.abbrev:Unknown}")
    private String commitIdAbbrev;

    @Value("${git.commit.message.short:Unknown}")
    private String commitMessageShort;

    @Value("${git.remote.origin.url:Unknown}")
    private String originUrl;

    @Value("${http.client.timeout:5}")
    private int httpClientTimeout;

    /**
     * Retrieves the version of the latest release from the GitHub repository.
     * This method sends a request to the GitHub releases URL and expects a redirection
     * to the latest release tag. If the redirection occurs, the version is extracted
     * from the redirection URL.
     *
     * @return An Optional containing the latest release version if available, otherwise an empty Optional.
     */
    private Optional<String> getLatestReleaseVersion() {
        try {
            HttpClient client = createHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_RELEASE_URL + LATEST_PATH))
                    .timeout(Duration.ofSeconds(httpClientTimeout))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpURLConnection.HTTP_OK) {
                LOGGER.warn("Expected a redirect but received HTTP 200 OK. " +
                        "The URL might be outdated or the server behavior changed.");
                return Optional.empty();
            }
            if (response.statusCode() != HttpURLConnection.HTTP_MOVED_PERM &&
                    response.statusCode() != HttpURLConnection.HTTP_MOVED_TEMP) {
                LOGGER.warn("No redirection detected; unexpected response code: {}", response.statusCode());
                return Optional.empty();
            }

            Optional<String> location = response.headers().firstValue("Location");
            if (location.isEmpty() || !location.get().contains(TAG_PATH)) {
                LOGGER.warn("Redirection detected, but the 'Location' header is missing " +
                        "or does not contain the expected tag path");
                return Optional.empty();
            }

            return location.map(loc -> loc.substring(loc.lastIndexOf(TAG_PATH) + TAG_PATH.length()));

        } catch (Exception e) {
            handleException(e, "fetching the latest release version");
        }
        // Return an empty Optional if we couldn't get the latest release version
        return Optional.empty();
    }

    /**
     * Returns the URL for the last commit by converting the origin URL to HTTPS and appending the commit ID.
     *
     * @return the URL pointing to the last commit on GitHub.
     */
    private Optional<String> getCommitUrl(String originUrl, String commitId) {
        if (originUrl == null || commitId == null) {
            LOGGER.warn("Origin URL or Commit ID is null. Cannot generate commit URL.");
            return Optional.empty();
        }
        String httpUrlBase = originUrl
                .replaceFirst(GIT_SSH_PREFIX, GIT_HTTPS_PREFIX)
                .replaceFirst("\\.git$", "/");
        return Optional.of(httpUrlBase + COMMIT_PATH + commitId);
    }

    /**
     * Retrieves Git repository metadata including the latest release information
     * and determines if an update is available.
     *
     * @return GitRepositoryMetadata containing current repository information.
     */
    public GitRepositoryMetadata getGitRepositoryMetadata() {
        Optional<String> latestReleaseVersionOpt = getLatestReleaseVersion();
        boolean updateAvailable = latestReleaseVersionOpt
                .map(latestVersion -> compareWithLatestVersion(buildVersion, latestVersion))
                .orElse(false);

        String latestReleaseUrl = latestReleaseVersionOpt
                .map(version -> BASE_RELEASE_URL + TAG_PATH + version)
                .orElse(BASE_RELEASE_URL + LATEST_PATH);

        Optional<String> latestCommitUrl = getCommitUrl(originUrl, commitId);

        return new GitRepositoryMetadata(
                buildVersion,
                branch,
                commitId,
                commitIdAbbrev,
                commitMessageShort,
                latestCommitUrl.orElse(null),
                latestReleaseUrl,
                latestReleaseVersionOpt.orElse(null),
                updateAvailable
        );
    }

    /**
     * Compares the current build version with the latest available version from GitHub.
     *
     * @param currentBuildVersion the current version of the build.
     * @param latestVersionTag    The latest version tag retrieved from an external source (e.g., GitHub).
     * @return true if an update is available, false otherwise.
     */
    private boolean compareWithLatestVersion(String currentBuildVersion, String latestVersionTag) {
        if (currentBuildVersion == null || latestVersionTag == null) {
            LOGGER.warn("Cannot determine if an update is available because one or both version strings are null. " +
                    "currentBuildVersion: {}, " +
                    "latestVersionTag: {}", currentBuildVersion, latestVersionTag);
            return false;
        }

        String latestVersion = latestVersionTag.startsWith(VERSION_PREFIX)
                ? latestVersionTag.substring(VERSION_PREFIX.length())
                : latestVersionTag;

        ComparableVersion v1 = new ComparableVersion(currentBuildVersion);
        ComparableVersion v2 = new ComparableVersion(latestVersion);
        return v1.compareTo(v2) < 0;
    }

    /**
     * Handles different types of exceptions that may occur during the HTTP request to fetch the latest version.
     * Logs detailed error messages based on the type of exception encountered.
     *
     * @param e The exception that occurred.
     */
    private void handleException(Exception e, String context) {
        if (e instanceof URISyntaxException) {
            LOGGER.warn("URI syntax error during {}: {}", context, e.getMessage());
        } else if (e instanceof ConnectException) {
            LOGGER.warn("Network error during {}: {}", context, e.getMessage());
        } else if (e instanceof IOException) {
            LOGGER.warn("I/O error during {}: {}", context, e.getMessage());
        } else if(e instanceof InterruptedException){
            LOGGER.warn("Request interrupted during {}: {}", context, e.getMessage());
            Thread.currentThread().interrupt();
        }else{
            LOGGER.warn("Unexpected error during {}: {}", context, e.getMessage());
        }
    }

    /**
     * Creates and configures an HttpClient instance with a specified connection timeout
     * and redirect handling strategy. The client is set to never follow redirects.
     *
     * @return a configured HttpClient instance.
     */
    private HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(httpClientTimeout))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * A record representing metadata related to the Git repository.
     *
     * @param buildVersion         The version of the current build.
     * @param branch               The branch name of the repository.
     * @param commitId             The full commit ID of the latest commit.
     * @param commitIdAbbrev       The abbreviated commit ID of the latest commit.
     * @param commitMessageShort   A short description of the latest commit message.
     * @param latestCommitUrl      The URL pointing to the latest commit on GitHub.
     * @param latestReleaseUrl     The URL pointing to the latest release on GitHub.
     * @param latestReleaseVersion The version tag of the latest release.
     * @param updateAvailable      A flag indicating whether an update is available.
     */
    public record GitRepositoryMetadata(
            String buildVersion,
            String branch,
            String commitId,
            String commitIdAbbrev,
            String commitMessageShort,
            String latestCommitUrl,
            String latestReleaseUrl,
            String latestReleaseVersion,
            boolean updateAvailable
    ){}
}