// SPDX-License-Identifier: MIT
pragma solidity >=0.6.0 <0.9.0;
pragma experimental ABIEncoderV2; // 开启高级 ABI 编码支持

library DataModel {
    enum Role { None, Supplier, Distributor, Retailer }
    enum FoodStatus { Created, InTransit, Delivered, OnSale }

    struct TraceRecord {
        address operator;
        Role role;
        string action;
        string location;
        string remark;
        uint256 timestamp;
    }

    struct Food {
        string foodId;
        string name;
        string origin;
        address currentOwner;
        FoodStatus status;
        bool exists;
        TraceRecord[] history;
    }
}
