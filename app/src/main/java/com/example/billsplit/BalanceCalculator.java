package com.example.billsplit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalanceCalculator {

    public static Map<String, Long> calculateNetBalances(List<String> members, List<Expense> expenses) {
        return calculateNetBalances(members, expenses, new ArrayList<>());
    }

    public static Map<String, Long> calculateNetBalances(List<String> members, List<Expense> expenses, List<Settlement> settlements) {
        Map<String, Long> balances = new HashMap<>();
        if (members == null) return balances;

        for (String m : members) {
            balances.put(m.trim(), 0L);
        }

        if (expenses != null) {
            for (Expense expense : expenses) {
                String paidBy = expense.getPaidBy();
                long totalAmountPaise = expense.getAmountPaise();

                Long currentPayerBalance = balances.get(paidBy);
                if (currentPayerBalance == null) currentPayerBalance = 0L;
                balances.put(paidBy, currentPayerBalance + totalAmountPaise);

                List<MemberSplit> splits = expense.getSplits();
                if (splits != null && !splits.isEmpty()) {
                    for (MemberSplit split : splits) {
                        String mName = split.getMemberName();
                        Long currentBal = balances.get(mName);
                        if (currentBal == null) currentBal = 0L;
                        balances.put(mName, currentBal - split.getSharePaise());
                    }
                } else {
                    long share = totalAmountPaise / (members.isEmpty() ? 1 : members.size());
                    for (String m : members) {
                        Long currentBal = balances.get(m);
                        if (currentBal == null) currentBal = 0L;
                        balances.put(m, currentBal - share);
                    }
                }
            }
        }

        // Apply settlements: payer gives money to receiver
        if (settlements != null) {
            for (Settlement s : settlements) {
                String payer = s.getPayer();
                String receiver = s.getReceiver();
                long amt = s.getAmountPaise();

                Long payerBal = balances.get(payer);
                if (payerBal == null) payerBal = 0L;
                balances.put(payer, payerBal + amt);

                Long receiverBal = balances.get(receiver);
                if (receiverBal == null) receiverBal = 0L;
                balances.put(receiver, receiverBal - amt);
            }
        }

        return balances;
    }

    public static List<Debt> calculateSimplifiedDebts(Map<String, Long> netBalances) {
        List<Debt> debts = new ArrayList<>();
        if (netBalances == null || netBalances.isEmpty()) {
            return debts;
        }

        List<String> debtors = new ArrayList<>();
        List<Long> debtorAmounts = new ArrayList<>();

        List<String> creditors = new ArrayList<>();
        List<Long> creditorAmounts = new ArrayList<>();

        for (Map.Entry<String, Long> entry : netBalances.entrySet()) {
            long bal = entry.getValue();
            if (bal < 0) {
                debtors.add(entry.getKey());
                debtorAmounts.add(-bal);
            } else if (bal > 0) {
                creditors.add(entry.getKey());
                creditorAmounts.add(bal);
            }
        }

        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            long owe = debtorAmounts.get(i);
            long receive = creditorAmounts.get(j);

            long settledAmount = Math.min(owe, receive);
            if (settledAmount > 0) {
                debts.add(new Debt(debtors.get(i), creditors.get(j), settledAmount));
            }

            debtorAmounts.set(i, owe - settledAmount);
            creditorAmounts.set(j, receive - settledAmount);

            if (debtorAmounts.get(i) == 0) i++;
            if (creditorAmounts.get(j) == 0) j++;
        }

        return debts;
    }
}
