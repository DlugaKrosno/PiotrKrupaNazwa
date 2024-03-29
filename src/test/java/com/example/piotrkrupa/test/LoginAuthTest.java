package com.example.piotrkrupa.test;

import com.example.piotrkrupa.MailService;
import com.example.piotrkrupa.base.BaseTest;
import com.example.piotrkrupa.testdata.LoginTestData;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class LoginAuthTest extends BaseTest {
    MailService mailService = new MailService();
    String correctAuthCode;

    @BeforeEach
    void goToAuthPage() throws InterruptedException {
        mailService.clearMail();

        navigateToLoginPage();
        loginPage.fillLoginPassword(LoginTestData.correctUser, LoginTestData.correctPassword)
                .submitLogin();

        if (loginPage.isElementExist("//*[@id='loginFormContainer']//*[contains(@class, 'message-text')]")) {
            loginPage.resolveCaptcha()
                     .fillLoginPassword(LoginTestData.correctUser, LoginTestData.correctPassword);
            Thread.sleep(500);
            loginPage.submitLogin();
        }

        navigateToAuthorisationPage();
        JSONObject verificationMail = mailService.getMail();
        correctAuthCode = authorisationPage.getVerificationCode(verificationMail);
    }

    @Test
    @DisplayName("Anulowanie autoryzacji dwuskladnikowej")
    public void cancelAuthorisation() {
        authorisationPage.cancelAuth();
        assertTrue(authorisationPage.isElementExist("//*[@id='loginFormContainer']"));
    }

    @ParameterizedTest
    @DisplayName("Niepoprawny kod autoryzacji")
    @ValueSource(strings = {"", "123456", "000000"})
    public void incorrectCode(String code) {

        authorisationPage.fillAuthCode(code)
                        .submitAuthCode();
        assertEquals("Podany kod jest nieprawidłowy", authorisationPage.getErrorText());
    }

    @Test
    @DisplayName("Ponowne wysłanie kodu autoryzacji i poprawne zalogoawnie użytkownika")
    public void resendCodeAndLogin() throws InterruptedException {
        mailService.clearMail();
        authorisationPage.resendEmail();

        assertEquals("Kod został ponownie wysłany", authorisationPage.getResendText());
        Thread.sleep(2000);

        JSONObject verificationMail = mailService.getMail();
        String newAuthCode = authorisationPage.getVerificationCode(verificationMail);

        assertNotEquals(correctAuthCode, newAuthCode);

        authorisationPage.fillAuthCode(newAuthCode)
                .submitAuthCode();

        navigateToHomePage();
        assertEquals(LoginTestData.correctUser, homePage.getLoggedUser());
    }

    @Test
    @DisplayName("Uzycie niewaznego kodu autoryzacji")
    public void oldCodeAndError() throws InterruptedException {
        mailService.clearMail();
        authorisationPage.resendEmail();
        Thread.sleep(2000);

        JSONObject verificationMail = mailService.getMail();
        String newAuthCode = authorisationPage.getVerificationCode(verificationMail);

        authorisationPage.fillAuthCode(newAuthCode)
                        .submitAuthCode();
        assertEquals("Podany kod jest nieprawidłowy", authorisationPage.getErrorText());
    }
}