package de.imi.mopat.dao.impl;

import de.imi.mopat.dao.QuestionnaireGroupDao;
import de.imi.mopat.model.QuestionnaireGroup;
import jakarta.persistence.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class QuestionnaireGroupDaoImpl extends MoPatDaoImpl<QuestionnaireGroup> implements QuestionnaireGroupDao {

    @Override
    @Transactional("MoPat")
    public Long getNextGroupId() {
        Query query = moPatEntityManager.createNativeQuery("SELECT nextval('group_id_sequence')");
        return ((Number) query.getSingleResult()).longValue();
    }
}
