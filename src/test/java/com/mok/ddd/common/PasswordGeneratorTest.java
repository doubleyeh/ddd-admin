package com.mok.ddd.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordGenerator 工具类测试")
class PasswordGeneratorTest {

    @Test
    @DisplayName("generateRandomPassword - 生成随机密码")
    void generateRandomPassword() {
        String password = PasswordGenerator.generateRandomPassword();
        assertNotNull(password);
        assertEquals(8, password.length());
        
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz0123456789";
        for (char c : password.toCharArray()) {
            assertTrue(allowedChars.indexOf(c) >= 0, "密码包含非法字符: " + c);
        }
    }
    
    @Test
    @DisplayName("generateRandomPassword - 多次生成不重复")
    void generateRandomPassword_Unique() {
        String p1 = PasswordGenerator.generateRandomPassword();
        String p2 = PasswordGenerator.generateRandomPassword();
        assertNotEquals(p1, p2);
    }
}
