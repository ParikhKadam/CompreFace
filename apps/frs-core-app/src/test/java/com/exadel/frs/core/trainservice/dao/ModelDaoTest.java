package com.exadel.frs.core.trainservice.dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.exadel.frs.core.trainservice.component.classifiers.LogisticRegressionExtendedClassifier;
import com.exadel.frs.core.trainservice.config.MongoTest;
import com.exadel.frs.core.trainservice.entity.Model;
import com.exadel.frs.core.trainservice.repository.ModelRepository;
import java.util.HashMap;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.EnabledIf;

@MongoTest
@Slf4j
@EnabledIf(expression = "#{environment.acceptsProfiles('integration-test')}")
public class ModelDaoTest {

    @Autowired
    private ModelRepository modelRepository;

    @AfterEach
    public void after() {
        modelRepository.deleteAll();
    }

    @Test
    public void classifierSave() {
        assertDoesNotThrow(this::saveTrainedModel);
    }

    @Test
    public void classifierGet() {
        val id = saveTrainedModel();

        assertTrue(modelRepository.findById(id).isPresent());
    }

    @Test
    public void classifierGetNotFound() {
        val id = saveTrainedModel();

        assertFalse(modelRepository.findById(id + "1").isPresent());
    }

    @Test
    public void delete() {
        var id = saveTrainedModel();

        assertEquals(1L, modelRepository.count());
        assertDoesNotThrow(() -> modelRepository.deleteById(id));
        assertEquals(0L, modelRepository.count());
    }

    @Test
    public void deleteWrong() {
        var id = saveTrainedModel();

        assertEquals(1L, modelRepository.count());
        assertDoesNotThrow(() -> modelRepository.deleteById(id + "1"));
        assertEquals(1L, modelRepository.count());
    }

    private String saveTrainedModel() {
        val x = new double[2][2];
        x[0][0] = 2;
        x[0][1] = 2;
        x[1][0] = 3;
        x[1][1] = 2;
        val y = new int[2];
        y[0] = 1;
        y[1] = 2;

        val labelMap = new HashMap<Integer, Pair<String, String>>();
        labelMap.put(1, Pair.of(UUID.randomUUID().toString(), "firstLabel"));
        labelMap.put(2, Pair.of(UUID.randomUUID().toString(), "secondLabel"));

        val classifier = new LogisticRegressionExtendedClassifier(labelMap);
        classifier.train(x, y);

        val model = Model.builder()
                         .classifier(classifier)
                         .id(UUID.randomUUID().toString())
                         .build();

        return modelRepository.save(model).getId();
    }
}