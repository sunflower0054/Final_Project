package com.office.monitoring.resident;

/** ResidentDeletionBlockedException의 역할을 담당한다. */
public class ResidentDeletionBlockedException extends RuntimeException {
    //거주자 삭제를 막기 위한 전용 예외처리
    public ResidentDeletionBlockedException(String message) {
        super(message);
    }
}
