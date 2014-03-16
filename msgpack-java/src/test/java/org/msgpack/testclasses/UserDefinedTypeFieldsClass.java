package org.msgpack.testclasses;

import org.junit.Ignore;
import org.msgpack.annotation.Beans;
import org.msgpack.annotation.Message;


@Ignore @Message @Beans
public class UserDefinedTypeFieldsClass {
    public NestedClass1 f0;

    public NestedClass2 f1;

    public UserDefinedTypeFieldsClass () {}

    public NestedClass1 getF0() {
        return f0;
    }

    public void setF0(NestedClass1 f0) {
        this.f0 = f0;
    }

    public NestedClass2 getF1() {
        return f1;
    }

    public void setF1(NestedClass2 f1) {
        this.f1 = f1;
    }

    @Override
    public boolean equals(Object o) {
	if (! (o instanceof UserDefinedTypeFieldsClass)) {
	    return false;
	}
	UserDefinedTypeFieldsClass that = (UserDefinedTypeFieldsClass) o;
	// f0
	if (f0 == null) {
	    if (that.f0 != null) {
		return false;
	    }
	}
	if (that.f0 != null) {
	    if (! f0.equals(that.f0)) {
		return false;
	    }
	}
	// f1
	if (f1 == null) {
	    if (that.f1 != null) {
		return false;
	    }
	}
	if (that.f1 != null) {
	    if (! f1.equals(that.f1)) {
		return false;
	    }
	}
	return true;
    }

    @Ignore @Message @Beans
    public static class NestedClass1 {
	public int f0;

	public String f1;

	public NestedClass1() {}

	public int getF0() {
	    return f0;
	}

	public void setF0(int f0) {
	    this.f0 = f0;
	}

	public String getF1() {
	    return f1;
	}

	public void setF1(String f1) {
	    this.f1 = f1;
	}

	@Override
	public boolean equals(Object o) {
	    if (! (o instanceof NestedClass1)) {
		return false;
	    }
	    NestedClass1 that = (NestedClass1) o;
	    if (f0 != that.f0) {
		return false;
	    }
	    if (f1 == null) {
		if (that.f1 != null) {
		    return false;
		}
	    }
	    if (that.f1 != null) {
		if (! f1.equals(that.f1)) {
		    return false;
		}
	    }
	    return true;
	}
    }

    @Ignore @Message @Beans
    public static class NestedClass2 {
	public int f0;

	public String f1;

	public NestedClass2() {}

	public int getF0() {
	    return f0;
	}

	public void setF0(int f0) {
	    this.f0 = f0;
	}

	public String getF1() {
	    return f1;
	}

	public void setF1(String f1) {
	    this.f1 = f1;
	}

	@Override
	public boolean equals(Object o) {
	    if (! (o instanceof NestedClass2)) {
		return false;
	    }
	    NestedClass2 that = (NestedClass2) o;
	    if (f0 != that.f0) {
		return false;
	    }
	    if (f1 == null) {
		if (that.f1 != null) {
		    return false;
		}
	    }
	    if (that.f1 != null) {
		if (! f1.equals(that.f1)) {
		    return false;
		}
	    }
	    return true;
	}
    }
}
