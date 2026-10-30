package com.example.billsplit;

import java.util.ArrayList;
import java.util.List;

public class SplitCalculator {

    public static List<MemberSplit> calculateEqualSplits(long totalAmountPaise, List<String> memberNames) {
        List<MemberSplit> splits = new ArrayList<>();
        if (memberNames == null || memberNames.isEmpty()) {
            return splits;
        }

        int memberCount = memberNames.size();
        long baseShare = totalAmountPaise / memberCount;
        long remainder = totalAmountPaise % memberCount;

        for (int i = 0; i < memberCount; i++) {
            long sharePaise = baseShare + (i < remainder ? 1 : 0);
            splits.add(new MemberSplit(memberNames.get(i), sharePaise));
        }

        return splits;
    }

    public static List<MemberSplit> calculateExactSplits(List<String> memberNames, List<Long> customAmountsPaise) {
        List<MemberSplit> splits = new ArrayList<>();
        if (memberNames == null || customAmountsPaise == null) {
            return splits;
        }

        for (int i = 0; i < memberNames.size(); i++) {
            long sharePaise = (i < customAmountsPaise.size()) ? customAmountsPaise.get(i) : 0;
            splits.add(new MemberSplit(memberNames.get(i), sharePaise));
        }

        return splits;
    }

    public static List<MemberSplit> calculatePercentageSplits(long totalAmountPaise, List<String> memberNames, List<Double> percentages) {
        List<MemberSplit> splits = new ArrayList<>();
        if (memberNames == null || percentages == null) {
            return splits;
        }

        long allocatedPaise = 0;
        int memberCount = memberNames.size();

        for (int i = 0; i < memberCount; i++) {
            double percent = (i < percentages.size()) ? percentages.get(i) : 0;
            long sharePaise = Math.round((totalAmountPaise * percent) / 100.0);
            splits.add(new MemberSplit(memberNames.get(i), sharePaise));
            allocatedPaise += sharePaise;
        }

        // Adjust paise rounding difference on first member if total matches percentage sum
        long diff = totalAmountPaise - allocatedPaise;
        if (diff != 0 && !splits.isEmpty()) {
            MemberSplit first = splits.get(0);
            splits.set(0, new MemberSplit(first.getMemberName(), first.getSharePaise() + diff));
        }

        return splits;
    }
}
