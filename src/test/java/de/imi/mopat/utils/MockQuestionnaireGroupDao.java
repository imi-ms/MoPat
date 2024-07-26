package de.imi.mopat.utils;

import de.imi.mopat.dao.QuestionnaireGroupDao;
import de.imi.mopat.model.Questionnaire;
import de.imi.mopat.model.QuestionnaireGroup;
import de.imi.mopat.model.enumeration.PermissionType;
import de.imi.mopat.model.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public class MockQuestionnaireGroupDao implements QuestionnaireGroupDao {

    private List<QuestionnaireGroup> groupStorage = new ArrayList<>();
    private long nextGroupId = 1;

    @Override
    public List<QuestionnaireGroup> getAllElements() {
        return new ArrayList<>(groupStorage);
    }

    @Override
    public Long getCount() {
        return (long) groupStorage.size();
    }

    @Override
    public void merge(QuestionnaireGroup element) {
        groupStorage.add(element);
    }

    @Override
    public void remove(QuestionnaireGroup element) {
        groupStorage.remove(element);
    }

    @Override
    public void grantRight(QuestionnaireGroup element, User user, PermissionType right, Boolean inheritance) {
    }

    @Override
    public void grantInheritedRight(QuestionnaireGroup element, User user, PermissionType right) {
    }

    @Override
    public void revokeRight(QuestionnaireGroup element, User user, PermissionType right, Boolean inheritance) {
    }

    @Override
    public void revokeInheritedRight(QuestionnaireGroup element, User user, PermissionType right) {
    }

    @Override
    public Long getNextGroupId() {
        return nextGroupId++;
    }

    @Override
    public Class<QuestionnaireGroup> getEntityClass() {
        return QuestionnaireGroup.class;
    }

    @Override
    public QuestionnaireGroup getElementById(Long id) {
        return groupStorage.stream()
                .filter(group -> group.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public QuestionnaireGroup getElementByUUID(String uuid) {
        return null;
    }

    @Override
    public Collection<QuestionnaireGroup> getElementsById(Collection<Long> ids) {
        List<QuestionnaireGroup> result = new ArrayList<>();
        for (Long id : ids) {
            groupStorage.stream()
                    .filter(group -> group.getId().equals(id))
                    .findFirst()
                    .ifPresent(result::add);
        }
        return result;
    }

    public Optional<QuestionnaireGroup> findByQuestionnaire(Questionnaire questionnaire) {
        return groupStorage.stream()
                .filter(group -> group.getQuestionnaire().equals(questionnaire))
                .findFirst();
    }

    public void clear() {
        groupStorage.clear();
        nextGroupId = 1;
    }
}


