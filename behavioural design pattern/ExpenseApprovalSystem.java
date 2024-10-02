package com.patterns.behavioral.chainofresponsibility;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpenseApprovalSystem {
    private static final Logger logger = LoggerFactory.getLogger(ExpenseApprovalSystem.class);

    public static void main(String[] args) {
        Approver teamLead = new TeamLead();
        Approver manager = new Manager();
        Approver director = new Director();
        
        teamLead.setNextApprover(manager);
        manager.setNextApprover(director);
        
        try {
            ExpenseRequest[] requests = {
                new ExpenseRequest(50.0, "Office supplies", Category.SUPPLIES),
                new ExpenseRequest(500.0, "Team building", Category.TEAM_BUILDING),
                new ExpenseRequest(5000.0, "Conference attendance", Category.TRAVEL),
                new ExpenseRequest(15000.0, "New server", Category.EQUIPMENT)
            };
            
            for (ExpenseRequest request : requests) {
                System.out.println("\nProcessing expense request: " + request);
                teamLead.approveExpense(request);
            }
        } catch (Exception e) {
            logger.error("Error processing expense requests", e);
        }
    }
}

enum Category {
    SUPPLIES, TEAM_BUILDING, TRAVEL, EQUIPMENT
}

class ExpenseRequest {
    private final String id;
    private final double amount;
    private final String description;
    private final Category category;

    public ExpenseRequest(double amount, String description, Category category) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.amount = amount;
        this.description = description;
        this.category = category;
    }

    public String getId() { return id; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }

    @Override
    public String toString() {
        return String.format("ExpenseRequest[ID: %s, Amount: $%.2f, Description: %s, Category: %s]",
                id, amount, description, category);
    }
}

abstract class Approver {
    protected Approver nextApprover;
    protected final double approvalLimit;
    protected final String role;
    protected static final Logger logger = LoggerFactory.getLogger(Approver.class);

    protected Approver(double approvalLimit, String role) {
        this.approvalLimit = approvalLimit;
        this.role = role;
    }

    public void setNextApprover(Approver nextApprover) {
        this.nextApprover = nextApprover;
    }

    public void approveExpense(ExpenseRequest request) {
        if (canApprove(request)) {
            approve(request);
        } else if (nextApprover != null) {
            logger.info("{} cannot approve request {}. Forwarding to {}", 
                    role, request.getId(), nextApprover.role);
            nextApprover.approveExpense(request);
        } else {
            logger.warn("No approver found for request: {}", request.getId());
            System.out.printf("❌ Expense request %s cannot be approved at any level%n", request.getId());
        }
    }

    protected abstract boolean canApprove(ExpenseRequest request);

    protected void approve(ExpenseRequest request) {
        logger.info("{} approved expense request: {}", role, request.getId());
        System.out.printf("✅ %s approved expense request %s for $%.2f%n", 
                role, request.getId(), request.getAmount());
    }
}

class TeamLead extends Approver {
    public TeamLead() {
        super(100.0, "Team Lead");
    }

    @Override
    protected boolean canApprove(ExpenseRequest request) {
        return request.getAmount() <= approvalLimit && 
               request.getCategory() == Category.SUPPLIES;
    }
}

class Manager extends Approver {
    public Manager() {
        super(1000.0, "Manager");
    }

    @Override
    protected boolean canApprove(ExpenseRequest request) {
        return request.getAmount() <= approvalLimit && 
               (request.getCategory() == Category.SUPPLIES || 
                request.getCategory() == Category.TEAM_BUILDING);
    }
}

class Director extends Approver {
    public Director() {
        super(10000.0, "Director");
    }

    @Override
    protected boolean canApprove(ExpenseRequest request) {
        return request.getAmount() <= approvalLimit;
    }
}
