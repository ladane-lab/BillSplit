const Expense = require('../models/Expense');

exports.getExpenses = async (req, res, next) => {
    try {
        const { groupId } = req.query;
        const query = groupId ? { group: groupId } : { createdBy: req.user._id };
        const expenses = await Expense.find(query).sort({ date: -1 });
        res.status(200).json({ success: true, count: expenses.length, data: expenses });
    } catch (error) {
        next(error);
    }
};

exports.getExpense = async (req, res, next) => {
    try {
        const expense = await Expense.findById(req.params.id);
        if (!expense) {
            return res.status(404).json({ success: false, message: 'Expense not found' });
        }
        res.status(200).json({ success: true, data: expense });
    } catch (error) {
        next(error);
    }
};

exports.createExpense = async (req, res, next) => {
    try {
        const { description, amountPaise, category, paidBy, groupId, splitType, splits } = req.body;

        if (!description || !amountPaise || !groupId) {
            return res.status(400).json({ success: false, message: 'Description, amount, and groupId are required' });
        }

        const expense = await Expense.create({
            description,
            amountPaise,
            category: category || 'Other',
            paidBy: paidBy || 'You',
            group: groupId,
            createdBy: req.user._id,
            splitType: splitType || 'EQUAL',
            splits: splits || []
        });

        res.status(201).json({ success: true, data: expense });
    } catch (error) {
        next(error);
    }
};

exports.updateExpense = async (req, res, next) => {
    try {
        let expense = await Expense.findById(req.params.id);
        if (!expense) {
            return res.status(404).json({ success: false, message: 'Expense not found' });
        }

        expense = await Expense.findByIdAndUpdate(req.params.id, req.body, {
            new: true,
            runValidators: true
        });

        res.status(200).json({ success: true, data: expense });
    } catch (error) {
        next(error);
    }
};

exports.deleteExpense = async (req, res, next) => {
    try {
        const expense = await Expense.findById(req.params.id);
        if (!expense) {
            return res.status(404).json({ success: false, message: 'Expense not found' });
        }

        await expense.deleteOne();
        res.status(200).json({ success: true, message: 'Expense deleted' });
    } catch (error) {
        next(error);
    }
};
