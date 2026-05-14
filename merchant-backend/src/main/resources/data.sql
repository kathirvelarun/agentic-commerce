-- Seed sample products on first boot (INSERT IGNORE skips if already present).
-- Translated from Python create_sample_data.py.

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Wireless Headphones','High-quality wireless headphones with noise cancellation',199.99,'Electronics','https://images.unsplash.com/photo-1505740420928-5e560c06d30e',50
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Wireless Headphones');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Smartphone','Latest smartphone with advanced camera and long battery life',699.99,'Electronics','https://images.unsplash.com/photo-1511707171634-5f897ff02aa9',30
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Smartphone');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Running Shoes','Comfortable running shoes for daily exercise',129.99,'Sports','https://images.unsplash.com/photo-1542291026-7eec264c27ff',75
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Running Shoes');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Coffee Maker','Automatic coffee maker with programmable timer',89.99,'Kitchen','https://images.unsplash.com/photo-1495474472287-4d71bcdd2085',25
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Coffee Maker');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Laptop','High-performance laptop for work and gaming',1299.99,'Electronics','https://images.unsplash.com/photo-1496181133206-80ce9b88a853',15
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Laptop');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Dumbells','Set of rubber dumbells for strength training',29.99,'Sports','https://images.unsplash.com/photo-1638536532686-d610adfc8e5c',100
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Dumbells');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Desk Lamp','LED desk lamp with adjustable brightness',49.99,'Home','https://images.unsplash.com/photo-1507473885765-e6ed057f782c',40
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Desk Lamp');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Bluetooth Speaker','Portable Bluetooth speaker with excellent sound quality',79.99,'Electronics','https://images.unsplash.com/photo-1608043152269-423dbba4e7e1',60
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bluetooth Speaker');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Water Bottle','Insulated stainless steel water bottle',24.99,'Sports','https://images.unsplash.com/photo-1602143407151-7111542de6e8',80
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Water Bottle');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Book: Python Programming','Comprehensive guide to Python programming',39.99,'Books','https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c',35
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Book: Python Programming');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Yoga Mat','Non-slip premium yoga mat for all exercises',29.99,'Sports','https://images.unsplash.com/photo-1544367567-0f2fcb009e0b',40
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Yoga Mat');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Air Fryer','Digital air fryer for healthy cooking',119.99,'Kitchen','https://images.unsplash.com/photo-1621955293419-2655068eee84',15
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Air Fryer');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'E-reader','e-reader for comfortable reading at home or on the go',45.99,'Electronics','https://images.unsplash.com/photo-1603406136476-85d8c3ec76a5',30
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'E-reader');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Cookbook: Healthy Meals','Collection of nutritious and delicious recipes',24.99,'Books','https://images.unsplash.com/photo-1513475382585-d06e58bcb0e0',45
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Cookbook: Healthy Meals');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Wireless Mouse','Ergonomic wireless mouse with long battery life',34.99,'Electronics','https://images.unsplash.com/photo-1527864550417-7fd91fc51a46',55
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Wireless Mouse');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Tennis Shoes','Casual tennis shoes for work or play',59.99,'Sports','https://images.unsplash.com/photo-1560769629-975ec94e6a86',20
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Tennis Shoes');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Stand Mixer','Professional stand mixer for baking enthusiasts',299.99,'Kitchen','https://images.unsplash.com/photo-1758565810987-ca8d617ea7be',8
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Stand Mixer');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Throw Pillows Set','Set of 4 decorative throw pillows',49.99,'Home','https://images.unsplash.com/photo-1728238321467-24afdb67ed36',35
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Throw Pillows Set');

INSERT INTO products (name, description, price, category, image_url, stock_quantity)
SELECT 'Book: Web Development','Modern web development techniques and best practices',44.99,'Books','https://images.unsplash.com/photo-1555066931-4365d14bab8c',28
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Book: Web Development');
