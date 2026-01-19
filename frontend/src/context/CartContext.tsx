import { createContext, useContext, useState, type ReactNode } from 'react';
import type { CartItem, Product } from '../types';

interface CartContextType {
    items: CartItem[];
    isOpen: boolean;
    addToCart: (product: Product) => void;
    removeFromCart: (id: number) => void;
    updateQuantity: (id: number, delta: number) => void;
    toggleCart: () => void;
    totalItems: number;
    totalPrice: number;
    view: 'store' | 'checkout' | 'tracking';
    setView: (view: 'store' | 'checkout' | 'tracking') => void;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export function CartProvider({ children }: { children: ReactNode }) {
    const [items, setItems] = useState<CartItem[]>([]);
    const [isOpen, setIsOpen] = useState(false);
    const [view, setView] = useState<'store' | 'checkout' | 'tracking'>('store');

    const addToCart = (product: Product) => {
        setItems(currentItems => {
            const existingItem = currentItems.find(item => item.id === product.id);
            if (existingItem) {
                return currentItems.map(item =>
                    item.id === product.id
                        ? { ...item, quantity: item.quantity + 1 }
                        : item
                );
            }
            return [...currentItems, { ...product, quantity: 1 }];
        });
        setIsOpen(true); // Abrir carrito al añadir
    };

    const removeFromCart = (id: number) => {
        setItems(currentItems => currentItems.filter(item => item.id !== id));
    };

    const updateQuantity = (id: number, delta: number) => {
        setItems(currentItems =>
            currentItems.map(item => {
                if (item.id === id) {
                    const newQuantity = Math.max(0, item.quantity + delta);
                    return { ...item, quantity: newQuantity };
                }
                return item;
            }).filter(item => item.quantity > 0)
        );
    };

    const toggleCart = () => setIsOpen(prev => !prev);

    const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
    const totalPrice = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);

    return (
        <CartContext.Provider value={{
            items,
            isOpen,
            addToCart,
            removeFromCart,
            updateQuantity,
            toggleCart,
            totalItems,
            totalPrice,
            view,
            setView
        }}>
            {children}
        </CartContext.Provider>
    );
}

export function useCart() {
    const context = useContext(CartContext);
    if (context === undefined) {
        throw new Error('useCart must be used within a CartProvider');
    }
    return context;
}
