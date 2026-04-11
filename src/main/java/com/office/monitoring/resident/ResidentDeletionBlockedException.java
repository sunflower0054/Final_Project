package com.office.monitoring.resident;

/** 거주자 데이터 조회·등록·수정·삭제 흐름을 담당하는 구성 요소. */
public class ResidentDeletionBlockedException extends RuntimeException {
    //거주자 삭제를 막기 위한 전용 예외처리
    public ResidentDeletionBlockedException(String message) {
        super(message);
    }
}
