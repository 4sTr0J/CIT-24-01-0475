package bmt;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BankController {

    private final BankService bankService;

    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Map<String, Object>>> getAccounts() {
        return ResponseEntity.ok(bankService.listAccounts());
    }

    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String accountNumber = (String) payload.get("accountNumber");
        double balance = Double.parseDouble(payload.get("balance").toString());

        if (name == null || name.trim().isEmpty() || accountNumber == null || accountNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Name and account number are required"));
        }

        Map<String, Object> account = bankService.createAccount(name, accountNumber, balance);
        if (account == null) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Could not create account"));
        }
        return ResponseEntity.ok(account);
    }

    @PostMapping("/accounts/{id}/deposit")
    public ResponseEntity<?> deposit(@PathVariable int id, @RequestBody Map<String, Object> payload) {
        double amount = Double.parseDouble(payload.get("amount").toString());
        String description = (String) payload.getOrDefault("description", "Deposit");
        
        if (amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Amount must be positive"));
        }

        bankService.deposit(id, amount, description);
        return ResponseEntity.ok(Map.of("message", "Deposit successful"));
    }

    @PostMapping("/accounts/{id}/withdraw")
    public ResponseEntity<?> withdraw(@PathVariable int id, @RequestBody Map<String, Object> payload) {
        double amount = Double.parseDouble(payload.get("amount").toString());
        String description = (String) payload.getOrDefault("description", "Withdrawal");

        if (amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Amount must be positive"));
        }

        try {
            bankService.withdraw(id, amount, description);
            return ResponseEntity.ok(Map.of("message", "Withdrawal successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/accounts/transfer")
    public ResponseEntity<?> transfer(@RequestBody Map<String, Object> payload) {
        int fromId = Integer.parseInt(payload.get("fromAccountId").toString());
        int toId = Integer.parseInt(payload.get("toAccountId").toString());
        double amount = Double.parseDouble(payload.get("amount").toString());

        if (amount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Amount must be positive"));
        }

        try {
            bankService.transfer(fromId, toId, amount);
            return ResponseEntity.ok(Map.of("message", "Transfer successful"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/accounts/{id}/transactions")
    public ResponseEntity<List<Map<String, Object>>> getTransactions(@PathVariable int id) {
        return ResponseEntity.ok(bankService.getTransactions(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(bankService.getSystemStats());
    }
}