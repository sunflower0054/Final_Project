package com.office.monitoring.resident;

public class ResidentDeletionBlockedException extends RuntimeException {
    //거주자 삭제를 막기 위한 전용 예외처리
    public ResidentDeletionBlockedException(String message) {
        super(message);
    }
}
