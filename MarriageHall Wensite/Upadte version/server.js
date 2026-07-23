const express = require('express');
const nodemailer = require('nodemailer');
const cors = require('cors');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.static(path.join(__dirname)));

// Email transporter (configure with your email)
const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.EMAIL_USER || 'your-email@gmail.com',
        pass: process.env.EMAIL_PASS || 'your-app-password'
    }
});

// Contact form endpoint
app.post('/api/contact', async (req, res) => {
    const { name, phone, eventType, date, message } = req.body;

    if (!name || !phone || !eventType) {
        return res.status(400).json({
            success: false,
            message: 'Please fill in all required fields'
        });
    }

    const mailOptions = {
        from: process.env.EMAIL_USER,
        to: process.env.EMAIL_USER,
        subject: `New Event Inquiry - ${eventType}`,
        html: `
            <h2>New Event Booking Inquiry</h2>
            <p><strong>Name:</strong> ${name}</p>
            <p><strong>Phone:</strong> ${phone}</p>
            <p><strong>Event Type:</strong> ${eventType}</p>
            <p><strong>Preferred Date:</strong> ${date || 'Not specified'}</p>
            <p><strong>Message:</strong> ${message || 'No message'}</p>
        `
    };

    try {
        await transporter.sendMail(mailOptions);
        res.json({ success: true, message: 'Thank you! We will contact you within 24 hours.' });
    } catch (error) {
        console.error('Email error:', error);
        res.status(500).json({ success: false, message: 'Something went wrong. Please try again.' });
    }
});

// Availability check endpoint
app.post('/api/check-availability', async (req, res) => {
    const { date, eventType } = req.body;

    // In a real app, you'd check against a database
    // For now, we'll just send a notification email
    const mailOptions = {
        from: process.env.EMAIL_USER,
        to: process.env.EMAIL_USER,
        subject: `Availability Check - ${date}`,
        html: `
            <h2>Availability Check Request</h2>
            <p><strong>Date:</strong> ${date}</p>
            <p><strong>Event Type:</strong> ${eventType}</p>
        `
    };

    try {
        await transporter.sendMail(mailOptions);
        res.json({ available: true, message: 'We will confirm availability within 24 hours.' });
    } catch (error) {
        res.status(500).json({ success: false, message: 'Error checking availability' });
    }
});

// Serve the main HTML file
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'MarriageHall.html'));
});

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});