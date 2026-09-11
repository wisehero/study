package dev.study.javalab.effectiveJava.chapter08;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResourceExampleTest {
    @Test
    void closesTheResourceAfterSuccessfulWork() throws IOException {
        var resource = new ResourceExample.Resource(null);
        assertEquals("읽기 성공", ResourceExample.read(resource, null));
        assertTrue(resource.closeCalled());
    }

    @Test
    void keepsWorkFailurePrimaryAndAttachesCloseFailure() {
        var readFailure = new IOException("읽기 실패");
        var closeFailure = new IOException("닫기 실패");
        var resource = new ResourceExample.Resource(closeFailure);
        var thrown = assertThrows(IOException.class,
                () -> ResourceExample.read(resource, readFailure));
        assertSame(readFailure, thrown);
        assertArrayEquals(new Throwable[]{closeFailure}, thrown.getSuppressed());
        assertTrue(resource.closeCalled());
    }

    @Test
    void closeFailurePreventsTheReturnValueFromReachingTheCaller() {
        var closeFailure = new IOException("닫기 실패");
        var resource = new ResourceExample.Resource(closeFailure);
        var thrown = assertThrows(IOException.class, () -> ResourceExample.read(resource, null));
        assertSame(closeFailure, thrown);
        assertEquals(0, thrown.getSuppressed().length);
        assertTrue(resource.closeCalled());
    }
}
