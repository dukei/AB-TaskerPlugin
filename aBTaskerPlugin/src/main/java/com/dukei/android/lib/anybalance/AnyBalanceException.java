package com.dukei.android.lib.anybalance;

public class AnyBalanceException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 675008758830426015L;

    public AnyBalanceException(String message) {
        super(message);
    }

    public AnyBalanceException(String message, Throwable th) {
        super(message, th);
    }

    public AnyBalanceException(Throwable th) {
        super(th);
    }
}
