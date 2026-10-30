const Group = require('../models/Group');

exports.getGroups = async (req, res, next) => {
    try {
        const groups = await Group.find({ owner: req.user._id });
        res.status(200).json({ success: true, count: groups.length, data: groups });
    } catch (error) {
        next(error);
    }
};

exports.getGroup = async (req, res, next) => {
    try {
        const group = await Group.findById(req.params.id);
        if (!group) {
            return res.status(404).json({ success: false, message: 'Group not found' });
        }
        res.status(200).json({ success: true, data: group });
    } catch (error) {
        next(error);
    }
};

exports.createGroup = async (req, res, next) => {
    try {
        const { name, type, members } = req.body;
        if (!name) {
            return res.status(400).json({ success: false, message: 'Group name is required' });
        }

        let memberList = members || ['You'];
        if (!memberList.includes('You')) {
            memberList.unshift('You');
        }

        const group = await Group.create({
            name,
            type: type || 'Flat 🏠',
            members: memberList,
            owner: req.user._id
        });

        res.status(201).json({ success: true, data: group });
    } catch (error) {
        next(error);
    }
};

exports.addMember = async (req, res, next) => {
    try {
        const { name } = req.body;
        if (!name) {
            return res.status(400).json({ success: false, message: 'Member name is required' });
        }

        const group = await Group.findById(req.params.id);
        if (!group) {
            return res.status(404).json({ success: false, message: 'Group not found' });
        }

        if (group.members.includes(name)) {
            return res.status(400).json({ success: false, message: 'Member already in group' });
        }

        group.members.push(name);
        await group.save();

        res.status(200).json({ success: true, data: group });
    } catch (error) {
        next(error);
    }
};

exports.removeMember = async (req, res, next) => {
    try {
        const { memberName } = req.body;
        const group = await Group.findById(req.params.id);
        if (!group) {
            return res.status(404).json({ success: false, message: 'Group not found' });
        }

        group.members = group.members.filter(m => m !== memberName);
        await group.save();

        res.status(200).json({ success: true, data: group });
    } catch (error) {
        next(error);
    }
};
