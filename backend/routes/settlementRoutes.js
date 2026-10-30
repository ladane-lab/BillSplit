const express = require('express');
const { getSettlements, createSettlement } = require('../controllers/settlementController');
const { protect } = require('../middleware/authMiddleware');

const router = express.Router();

router.use(protect);

router.route('/')
    .get(getSettlements)
    .post(createSettlement);

module.exports = router;
