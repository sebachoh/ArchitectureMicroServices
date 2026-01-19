export interface Product {
    id: number;
    name: string;
    description: string;
    price: number;
    image: string;
    rating: number;
}

export interface CartItem extends Product {
    quantity: number;
}
