package com.hsbc.finalproject.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalAssetDTO {
    private Double totalAsset;
    private Double holdingValue;
    private Double cashValue;
    private Double totalReturn;
}
