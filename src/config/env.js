import 'dotenv/config';

export const PORT = Number(process.env.PORT || 3000);
export const FRONTEND_URL = process.env.FRONTEND_URL || 'http://localhost:5173';
export const JWT_SECRET = process.env.JWT_SECRET || '';
export const TELEFONO_PELUQUERIA = process.env.TELEFONO_PELUQUERIA || '';
