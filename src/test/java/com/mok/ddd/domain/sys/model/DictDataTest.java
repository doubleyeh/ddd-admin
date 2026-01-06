package com.mok.ddd.domain.sys.model;

import com.mok.ddd.application.sys.dto.dict.DictDataSaveDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class DictDataTest {

    private DictData createTestDictData(String label, String value) {
        try {
            var constructor = DictData.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            DictData dictData = constructor.newInstance();
            setField(dictData, "label", label);
            setField(dictData, "value", value);
            return dictData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = DictData.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Nested
    @DisplayName("create 方法测试")
    class CreateTests {
        @Test
        void create_Success() {
            DictDataSaveDTO dto = new DictDataSaveDTO();
            dto.setTypeCode("test_type");
            dto.setLabel("Label");
            dto.setValue("Value");
            dto.setSort(1);
            dto.setIsDefault(true);

            DictData dictData = DictData.create(dto);

            assertNotNull(dictData);
            assertEquals("test_type", dictData.getTypeCode());
            assertEquals("Label", dictData.getLabel());
            assertEquals("Value", dictData.getValue());
            assertEquals(1, dictData.getSort());
            assertTrue(dictData.getIsDefault());
        }
    }

    @Nested
    @DisplayName("updateInfo 方法测试")
    class UpdateInfoTests {
        @Test
        void updateInfo_ShouldUpdateFields() {
            DictData dictData = createTestDictData("Old Label", "old_value");

            DictDataSaveDTO dto = new DictDataSaveDTO();
            dto.setLabel("New Label");
            dto.setValue("new_value");
            dto.setSort(10);

            dictData.updateInfo(dto);

            assertEquals("New Label", dictData.getLabel());
            assertEquals("new_value", dictData.getValue());
            assertEquals(10, dictData.getSort());
        }
    }
}
