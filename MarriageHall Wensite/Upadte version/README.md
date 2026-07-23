# Subhan Marriage Hall - Fort Abbas, Pakistan

A professional, responsive website for Subhan Marriage Hall with full backend integration.

## Features

- ✅ Modern, responsive design with mobile menu
- ✅ Contact form with email notifications
- ✅ WhatsApp integration (03463630243)
- ✅ Loading animation
- ✅ Smooth scrolling navigation
- ✅ SEO optimized
- ✅ Cross-browser compatible

## Setup Instructions

### 1. Install Dependencies

```bash
npm install
```

### 2. Configure Email (for contact form)

1. Copy `.env.example` to `.env`
2. Update the values with your Gmail credentials:
   - `EMAIL_USER`: Your Gmail address
   - `EMAIL_PASS`: Your Gmail App Password (NOT your regular password)

**To get a Gmail App Password:**
- Go to Google Account Settings → Security
- Enable 2-Factor Authentication
- Go to App Passwords → Generate new password
- Use the generated password in `.env`

### 3. Run the Server

```bash
# Development mode (with auto-restart)
npm run dev

# Production mode
npm start
```

The website will be available at `http://localhost:3000`

## Project Structure

```
Marriage Hall Website/
├── MarriageHall.html    # Main website (frontend)
├── server.js            # Node.js backend server
├── package.json         # Project dependencies
├── .env.example         # Environment variables template
├── README.md            # This file
└── images/
    ├── hero_background.png
    ├── about_section.png
    ├── gallery_1.png
    ├── gallery_2.png
    ├── gallery_3.png
    └── gallery_4.png
```

## API Endpoints

### POST /api/contact
Submit event booking inquiry
- **Body:** `{ name, phone, eventType, date, message }`
- **Response:** `{ success: true, message: "..." }`

### POST /api/check-availability
Check date availability
- **Body:** `{ date, eventType }`
- **Response:** `{ available: true, message: "..." }`

## Deployment

### Deploy to Render.com (Free)

1. Create account at [render.com](https://render.com)
2. Create new Web Service
3. Connect your GitHub repository
4. Set build command: `npm install`
5. Set start command: `npm start`
6. Add environment variables in Render dashboard

### Deploy to Railway.app (Free)

1. Create account at [railway.app](https://railway.app)
2. Create new project from GitHub
3. Add environment variables in Railway dashboard

## Contact

- **Phone:** +92-346-3630243
- **WhatsApp:** [Chat Now](https://wa.me/923463630243)
- **Location:** Fort Abbas, Sahiwal, Punjab, Pakistan

## License

MIT License - Feel free to use and modify for your own marriage hall!