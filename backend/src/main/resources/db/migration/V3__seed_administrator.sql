INSERT INTO administrator_account (username, password_hash, security_answer_hash, failed_attempts)
SELECT 'administrator2026-1', '$2a$10$8pfbSfVaSV4ObXFSapUki.CHGP1K1PRLgOQSReupDXQ8pBGE7vv5e', '$2a$10$3c6vfZKpxLQg9zZSRkWmlux9SL9efnLab7LY5Rsi5zVF.Vdb.V1mi', 0
WHERE NOT EXISTS (
    SELECT 1 FROM administrator_account WHERE username = 'administrator2026-1'
);
