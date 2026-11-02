package de.imi.mopat.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import javax.swing.text.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
    AppConfig.class,
    ApplicationSecurityConfig.class,
    MvcWebApplicationInitializer.class,
    PersistenceConfig.class
})
@TestPropertySource(
    locations = "classpath:mopat-test.properties",
    properties = "de.imi.mopat.isDemoInstance=false"
)
@WebAppConfiguration
public class DemoModeNotActiveTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .apply(springSecurity())
            .build();
    }

    @Test
    public void shouldNotShowDemoLoginValues() throws Exception {
        mockMvc.perform(get("/mobile/user/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("value=\"admin\""))))
            .andExpect(content().string(not(containsString("value=\"admin123\""))))
            .andExpect(content().string(not(containsString("readonly=\"true\""))));
    }
}
