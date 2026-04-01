/*
package com.office.monitoring.test;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;


public class coolsmsTest {

        public static void main(String[] args) {
            // 1. 서비스 객체 생성 (전달해주신 키 입력)
            DefaultMessageService messageService = NurigoApp.INSTANCE.initialize(
                    "NCS5NJ2B47BSLF3X",
                    "OM3CAC3IE8C9ZG5J5HHZDYBBLCYYUGKU",
                    "https://api.coolsms.co.kr"
            );

            // 2. 메시지 생성
            Message message = new Message();
            message.setFrom("01083763942"); // 하이픈(-) 없이 입력
            message.setTo("01083763942");   // 본인 번호로 테스트
            message.setText("솔라피 API 테스트 문자입니다! 🚀");

            // 3. 발송 요청 및 결과 확인
            SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));
            System.out.println("발송 결과: " + response);
        }
    }

*/
