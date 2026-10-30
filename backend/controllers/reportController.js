const Expense = require('../models/Expense');

exports.getReports = async (req, res, next) => {
    try {
        const { groupId } = req.query;
        const query = groupId ? { group: groupId } : { createdBy: req.user._id };
        const expenses = await Expense.find(query);

        let totalSpendingPaise = 0;
        const categoryTotals = {};
        const memberTotals = {};

        expenses.forEach(exp => {
            totalSpendingPaise += exp.amountPaise;

            categoryTotals[exp.category] = (categoryTotals[exp.category] || 0) + exp.amountPaise;
            memberTotals[exp.paidBy] = (memberTotals[exp.paidBy] || 0) + exp.amountPaise;
        });

        res.status(200).json({
            success: true,
            data: {
                totalSpendingPaise,
                categoryTotals,
                memberTotals,
                expenseCount: expenses.length
            }
        });
    } catch (error) {
        next(error);
    }
};
