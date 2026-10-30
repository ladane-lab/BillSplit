const express = require('express');
const { getGroups, getGroup, createGroup, addMember, removeMember } = require('../controllers/groupController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

router.use(protect);

router.route('/')
    .get(getGroups)
    .post(createGroup);

router.route('/:id')
    .get(getGroup);

router.post('/:id/members', addMember);
router.delete('/:id/members', removeMember);

module.exports = router;
