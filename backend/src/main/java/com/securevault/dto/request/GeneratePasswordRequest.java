package com.securevault.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class GeneratePasswordRequest {

    @Min(value = 8, message = "Password length must be at least 8 characters")
    @Max(value = 64, message = "Password length cannot exceed 64 characters")
    private int length = 16;

    private boolean useUpper = true;
    private boolean useLower = true;
    private boolean useNumbers = true;
    private boolean useSpecial = true;

    public GeneratePasswordRequest() {}

    public GeneratePasswordRequest(int length, boolean useUpper, boolean useLower, boolean useNumbers, boolean useSpecial) {
        this.length = length;
        this.useUpper = useUpper;
        this.useLower = useLower;
        this.useNumbers = useNumbers;
        this.useSpecial = useSpecial;
    }

    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }

    public boolean isUseUpper() { return useUpper; }
    public void setUseUpper(boolean useUpper) { this.useUpper = useUpper; }

    public boolean isUseLower() { return useLower; }
    public void setUseLower(boolean useLower) { this.useLower = useLower; }

    public boolean isUseNumbers() { return useNumbers; }
    public void setUseNumbers(boolean useNumbers) { this.useNumbers = useNumbers; }

    public boolean isUseSpecial() { return useSpecial; }
    public void setUseSpecial(boolean useSpecial) { this.useSpecial = useSpecial; }
}
