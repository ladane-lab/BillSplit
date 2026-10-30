const Settlement = require('../models/Settlement');

exports.getSettlements = async (req, res, next) => {
    try {
        const { groupId } = req.query;
        const query = groupId ? { group: groupId } : { createdBy: req.user._id };
        const settlements = await Settlement.find(query).sort({ date: -1 });
        res.status(200).json({ success: true, count: settlements.length, data: settlements });
    } catch (error) {
        next(error);
    }
};

exports.createSettlement = async (req, res, next) => {
    try {
        const { groupId, payer, receiver, amountPaise } = req.body;

        if (!groupId || !payer || !receiver || !amountPaise) {
            return res.status(400).json({ success: false, message: 'All settlement fields are required' });
        }

        const settlement = await Settlement.create({
            group: groupId,
            payer,
            receiver,
            amountPaise,
            createdBy: req.user._id
        });

        res.status(201).json({ success: true, data: settlement });
    } catch (error) {
        next(error);
    }
};
