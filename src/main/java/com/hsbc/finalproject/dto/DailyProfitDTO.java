package com.hsbc.finalproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyProfitDTO {
    private Double profit;
    private Double profitPercentage;
}
