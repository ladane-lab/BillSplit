const mongoose = require('mongoose');

const splitSchema = new mongoose.Schema({
    memberName: {
        type: String,
        required: true
    },
    sharePaise: {
        type: Number,
        required: true
    }
});

const expenseSchema = new mongoose.Schema({
    description: {
        type: String,
        required: [true, 'Please add a description'],
        trim: true
    },
    amountPaise: {
        type: Number,
        required: [true, 'Please add an amount in paise']
    },
    category: {
        type: String,
        default: 'Other'
    },
    paidBy: {
        type: String,
        required: [true, 'Please specify who paid']
    },
    group: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Group',
        required: true
    },
    createdBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },
    splitType: {
        type: String,
        enum: ['EQUAL', 'EXACT', 'PERCENT'],
        default: 'EQUAL'
    },
    splits: [splitSchema],
    date: {
        type: Date,
        default: Date.now
    }
});

module.exports = mongoose.model('Expense', expenseSchema);
