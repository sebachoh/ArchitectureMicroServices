import axios from 'axios';
import { API_BASE_URL } from '../config';
import type { Product } from '../types';

// Import images for mapping
import empanadaImg from '../assets/img/products/1-empanada.png';
import arepaImg from '../assets/img/products/2-arepa.jpg';
import bandejaImg from '../assets/img/products/3-bandeja.jpg';

const api = axios.create({
    baseURL: API_BASE_URL,
});

/**
 * Maps backend products to include images and ratings for the UI.
 */
const mapProduct = (data: any): Product => {
    let image = empanadaImg;
    let rating = 4.5;

    if (data.name.toLowerCase().includes('empanada')) {
        image = empanadaImg;
        rating = 4.9;
    } else if (data.name.toLowerCase().includes('arepa')) {
        image = arepaImg;
        rating = 4.8;
    } else if (data.name.toLowerCase().includes('bandeja')) {
        image = bandejaImg;
        rating = 5.0;
    }

    return {
        ...data,
        image,
        rating,
    };
};

export const catalogueService = {
    getProducts: async (): Promise<Product[]> => {
        try {
            const response = await api.get('/api/products');
            return response.data.map(mapProduct);
        } catch (error) {
            console.error('Error fetching products:', error);
            throw error;
        }
    },
};

export const panierService = {
    addToCart: async (productId: number, quantity: number): Promise<void> => {
        try {
            await api.post(`/cart/add`, null, {
                params: { productId, quantity },
            });
        } catch (error) {
            console.error('Error adding to backend cart:', error);
            throw error;
        }
    },
    clearCart: async (): Promise<void> => {
        try {
            await api.delete('/cart');
        } catch (error) {
            console.error('Error clearing backend cart:', error);
            throw error;
        }
    },
};

export default api;
