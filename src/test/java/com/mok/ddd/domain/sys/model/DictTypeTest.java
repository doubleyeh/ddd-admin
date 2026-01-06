package com.mok.ddd.domain.sys.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class DictTypeTest {

    private DictType createTestDictType(String name, String code, Boolean isSystem) {
        try {
            var constructor = DictType.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            DictType dictType = constructor.newInstance();
            setField(dictType, "name", name);
            setField(dictType, "code", code);
            setField(dictType, "isSystem", isSystem);
            return dictType;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = DictType.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Nested
    @DisplayName("create 方法测试")
    class CreateTests {
        @Test
        void create_Success() {
            DictType dictType = DictType.create("Test Type", "test_type", 1, "Remark");

            assertNotNull(dictType);
            assertEquals("Test Type", dictType.getName());
            assertEquals("test_type", dictType.getCode());
            assertEquals(1, dictType.getSort());
            assertEquals("Remark", dictType.getRemark());
            assertFalse(dictType.getIsSystem());
        }
    }

    @Nested
    @DisplayName("updateInfo 方法测试")
    class UpdateInfoTests {
        @Test
        void updateInfo_ShouldUpdateFields() {
            DictType dictType = createTestDictType("Old Name", "old_code", false);
            dictType.updateInfo("New Name", 10, "New Remark");

            assertEquals("New Name", dictType.getName());
            assertEquals("old_code", dictType.getCode());
            assertEquals(10, dictType.getSort());
            assertEquals("New Remark", dictType.getRemark());
        }
    }
}
