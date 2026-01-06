package com.mok.ddd.domain.sys.model;

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
            DictData dictData = DictData.create("test_type", "Label", "Value", 1, "css", "list", true, "remark");

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
            dictData.updateInfo("New Label", "new_value", 10, "css", "list", false, "remark");

            assertEquals("New Label", dictData.getLabel());
            assertEquals("new_value", dictData.getValue());
            assertEquals(10, dictData.getSort());
        }
    }
}
