-- 書籍のダミーデータ
INSERT INTO book (isbn, title, price, stock) VALUES
('9784297100339', '達人プログラマー', 3200, 10),
('9784798157622', 'Clean Architecture', 3400, 5),
('9784774193136', 'ドメイン駆動設計入門', 3000, 8);

-- 顧客のダミーデータ
INSERT INTO customer (id, name, email) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '山田太郎', 'taro.yamada@example.com'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '鈴木花子', 'hanako.suzuki@example.com');

-- 注文のダミーデータ
-- 注文1: 山田太郎が達人プログラマーを1冊、Clean Architectureを1冊注文
INSERT INTO "order" (id, customer_id, order_datetime, status) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '2025-09-28 10:00:00', 'UNSHIPPED');

INSERT INTO order_item (order_id, book_isbn, quantity) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '9784297100339', 1),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '9784798157622', 1);

-- 注文2: 鈴木花子がドメイン駆動設計入門を2冊注文
INSERT INTO "order" (id, customer_id, order_datetime, status) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '2025-09-28 11:00:00', 'UNSHIPPED');

INSERT INTO order_item (order_id, book_isbn, quantity) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', '9784774193136', 2);
