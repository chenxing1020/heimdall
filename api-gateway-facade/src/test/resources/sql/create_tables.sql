CREATE TABLE db_basic.t_payment
(
    pk            INT          NOT NULL AUTO_INCREMENT,
    paymentNo     VARCHAR(25)  NOT NULL DEFAULT '' COMMENT '付款单号',
    bizId         VARCHAR(25)  NOT NULL DEFAULT '' COMMENT '内部业务ID',
    `description` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '交易描述',
    openId        VARCHAR(128) NOT NULL DEFAULT '' COMMENT '用户标识',
    amount        INT          NOT NULL DEFAULT '0' COMMENT '总金额',
    payed         TINYINT      NOT NULL DEFAULT '0' COMMENT '删除状态 0-未支付；1-已支付',
    createTime    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    modifyTime    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    deleteStatus  TINYINT      NOT NULL DEFAULT '0' COMMENT '删除状态 0-未删除；1-已删除',
    PRIMARY KEY (pk),
    UNIQUE KEY uk_paymentNo (paymentNo),
    KEY idx_bizId (bizId)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4 COMMENT ='基础系统|交易信息|xchen|20210731';


CREATE TABLE db_basic.t_wxpay_tx_log
(
    pk            INT          NOT NULL AUTO_INCREMENT,
    appid         VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '应用ID',
    outTradeNo    VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '商户订单号',
    transactionId VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '微信支付订单号',
    tradeType     VARCHAR(16)  NOT NULL DEFAULT '' COMMENT '交易类型',
    tradeState    VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '交易状态',
    successTime   DATETIME     NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '交易成功',
    openid        VARCHAR(128) NOT NULL DEFAULT '' COMMENT '用户标识',
    amount        INT          NOT NULL DEFAULT '0' COMMENT '总金额',
    payerAmount   INT          NOT NULL DEFAULT '0' COMMENT '用户支付金额',
    createTime    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleteStatus  TINYINT      NOT NULL DEFAULT '0' COMMENT '删除状态 0-未删除；1-已删除',
    PRIMARY KEY (pk),
    UNIQUE KEY uk_outTradeNo_tradeState (outTradeNo, tradeState)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4 COMMENT ='基础系统|微信支付流水|xchen|20210730';
