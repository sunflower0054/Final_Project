import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin") // 관리자 페이지 공통 경로
public class AdminViewController {

    // 브라우저 주소창에 /admin/stats 를 입력하면 이 메서드가 실행됩니다.
    @GetMapping("/stats")
    public String showAdminStatsPage() {

        // 반환하는 문자열은 '보여줄 HTML 파일의 이름'입니다. (.html 확장자는 생략)
        return "admin/adminStats";
    }
}