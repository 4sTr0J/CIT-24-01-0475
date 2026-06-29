package bmt;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("balance", bankService.getBalance());

        return "index";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam double amount) {

        bankService.deposit(amount);

        return "redirect:/";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam double amount) {

        bankService.withdraw(amount);

        return "redirect:/";
    }
}