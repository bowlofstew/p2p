package com.rp.p2p.loan_selector;

import com.rp.p2p.model.*;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @Deprecated
 */
public class InCodeLoansSelector implements LoanSelector {
    private final static Logger logger_ =Logger.getLogger(InCodeLoansSelector.class);

    private Set<HomeOwnership> VALID_HOMEOWNERSHIP = new HashSet<HomeOwnership>();

    {
        VALID_HOMEOWNERSHIP.add(HomeOwnership.RENT);
        VALID_HOMEOWNERSHIP.add(HomeOwnership.MORTGAGE);
        VALID_HOMEOWNERSHIP.add(HomeOwnership.OWN);//Is this okay?
    }

    private Set<IncomeVerification> VALID_INCOME_VERIFICATION = new HashSet<IncomeVerification>();

    {
        VALID_INCOME_VERIFICATION.add(IncomeVerification.SOURCE_VERIFIED);
        VALID_INCOME_VERIFICATION.add(IncomeVerification.VERIFIED);
    }

    private Set<LoanPurpose> VALID_LOAN_PURPOSE = new HashSet<LoanPurpose>();

    {
        VALID_LOAN_PURPOSE.add(LoanPurpose.CREDIT_CARD);
        VALID_LOAN_PURPOSE.add(LoanPurpose.DEBT_CONSOLIDATION);
        VALID_LOAN_PURPOSE.add(LoanPurpose.HOME_IMPROVEMENT);//Is this okay?
    }

    private Set<LoanGrade> VALID_GRADE = new HashSet<LoanGrade>();

    {
        VALID_GRADE.add(LoanGrade.A);
        VALID_GRADE.add(LoanGrade.B);
        VALID_GRADE.add(LoanGrade.C);
        VALID_GRADE.add(LoanGrade.D);
    }

    private Set<Integer> VALID_TERM = new HashSet<Integer>();

    {
        VALID_TERM.add(36);
        VALID_TERM.add(60);
    }

    public boolean select(Set<Long> allInvestedLoans, LoanListing loan) {
        int brokenRules = 0;

        if (!VALID_HOMEOWNERSHIP.contains(loan.getHomeOwnership())) {
            logger_.info("Failed VALID_HOMEOWNERSHIP [" + loan.getHomeOwnership() + "] " + loan.getId());
            brokenRules++;
        }
        if (((loan.getLoanAmnt() / loan.getAnnualInc()) * 100.0) > 17.0) {
            logger_.info("Failed ((loan.getLoanAmnt()/loan.getAnnualInc())*100.0) [" + ((loan.getLoanAmnt() / loan.getAnnualInc()) * 100.0) + "] " + loan.getId());
            brokenRules++;
        }
        if ((loan.getAnnualInc()) < 54000.0) {
            logger_.info("Failed (loan.getAnnualInc()) [" + loan.getAnnualInc() + "] " + loan.getId());
            brokenRules++;
        }
        if (!VALID_TERM.contains(loan.getTerm())) {
            logger_.info("Failed VALID_TERM [" + loan.getTerm() + "] " + loan.getId());
            brokenRules++;
        }
        if (loan.getEmpTitle() == null) {
            logger_.info("Failed loan.getEmpTitle() [" + loan.getEmpTitle() + "] " + loan.getId());
            brokenRules++;
        }
        if (loan.getCreditInfo().getDti() > 21.0) {
            logger_.info("Failed loan.getCreditInfo().getDti() [" + loan.getCreditInfo().getDti() + "] " + loan.getId());
            brokenRules++;
        }
        if (loan.getCreditInfo().getMthsSinceLastDelinq() != null && loan.getCreditInfo().getMthsSinceLastDelinq() < (12 * 2)) {
            logger_.info("Failed loan.getCreditInfo().getMthsSinceLastDelinq() [" + loan.getCreditInfo().getMthsSinceLastDelinq() + "] " + loan.getId());
            brokenRules++;
        }
        if (loan.getCreditInfo().getPubRec() != null && loan.getCreditInfo().getPubRec() > 0) {
            logger_.info("Failed loan.getCreditInfo().getPubRec() [" + loan.getCreditInfo().getPubRec() + "] " + loan.getId());
            brokenRules++;
        }
        if (loan.getLoanAmnt() > 25000) {
            logger_.info("Failed loan.getLoanAmnt() [" + loan.getLoanAmnt() + "] " + loan.getId());
            brokenRules++;
        }
        if (loan.getCreditInfo().getRevolUtil() != null && loan.getCreditInfo().getRevolUtil() > 55.0) {
            logger_.info("Failed loan.getCreditInfo().getRevolUtil() [" + loan.getCreditInfo().getRevolUtil() + "] " + loan.getId());
            brokenRules++;
        }
        if (!VALID_LOAN_PURPOSE.contains(loan.getPurpose())) {
            logger_.info("Failed VALID_LOAN_PURPOSE [" + loan.getPurpose() + "] " + loan.getId());
            brokenRules++;
        }
        if (!VALID_GRADE.contains(loan.getGrade())) {
            logger_.info("Failed VALID_GRADE [" + loan.getGrade() + "] " + loan.getId());
            brokenRules++;
        }
        if ((loan.getIntRate() < 9.0)) {
            logger_.info("Failed INT_RATE [" + loan.getIntRate() + "] " + loan.getId());
            brokenRules++;
        }
        if ((loan.getIntRate() + 5.0) < (loan.getExpDefaultRate() + loan.getServiceFeeRate()))//MIN_INT_RATE
        {
            logger_.info("Failed MIN_INT_RATE [" + loan.getIntRate() + "] " + loan.getId());
            brokenRules++;
        }
        if (allInvestedLoans.contains(loan.getId())) {
            logger_.info("Failed allInvestedLoans.contains(loan.getId())" + " " + loan.getId());
            brokenRules++;
        }
        if (brokenRules > 0) {
            logger_.info("Loan [" + loan.getId() + "] broke [" + brokenRules + "] rules");
        }
        else
        {
            logger_.info("Loan [" + loan.getId() + "] succeeded");
        }
        return brokenRules == 0;
    }
}
