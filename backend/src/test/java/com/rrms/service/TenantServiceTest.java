

package com.rrms.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TenantServiceTest {

    @Autowired
    private TenantService tenantService;




    // UTCID01: Input là null (Abnormal)
    @Test
    public void testIsValidCitizenId_Null_ReturnsFalse() {
        assertFalse(tenantService.isValidCitizenId(null), "Null phải false");
    }

    // UTCID02: Input chứa chữ cái (Abnormal)
    @Test
    public void testIsValidCitizenId_ContainsLetters_ReturnsFalse() {
        assertFalse(tenantService.isValidCitizenId("079A1234"), "Chứa chữ cái phải false");
    }

    // UTCID03: Input chứa ký tự đặc biệt (Abnormal)
    @Test
    public void testIsValidCitizenId_SpecialChars_ReturnsFalse() {
        assertFalse(tenantService.isValidCitizenId("079@1234"), "Ký tự ĐB phải false");
    }

    // UTCID04: Input chuỗi rỗng (Abnormal)
    @Test
    public void testIsValidCitizenId_EmptyString_ReturnsFalse() {
        assertFalse(tenantService.isValidCitizenId(""), "Chuỗi rỗng phải false");
    }

    // UTCID05: Input hợp lệ 12 số (Normal)
    @Test
    public void testIsValidCitizenId_ValidId_ReturnsTrue() {
        assertTrue(tenantService.isValidCitizenId("079205012345"), "ID đúng chuẩn phải true");
    }
}