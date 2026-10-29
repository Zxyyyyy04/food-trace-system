// SPDX-License-Identifier: MIT
pragma solidity >=0.6.0 <0.9.0;
pragma experimental ABIEncoderV2; // 开启高级 ABI 编码支持

import "./DataModel.sol";

contract FoodTraceability {
    address public admin;
    mapping(address => DataModel.Role) public roles;
    mapping(string => DataModel.Food) private foods; 
    string[] private foodIds;              

    event RoleAssigned(address indexed account, DataModel.Role role);
    event FoodCreated(string foodId, address supplier);

    // 新版本构造函数写法
    constructor() public {
        admin = msg.sender;
        roles[msg.sender] = DataModel.Role.Supplier; 
    }

    modifier onlyRole(DataModel.Role _role) {
        require(roles[msg.sender] == _role, "Permission denied: Invalid role");
        _;
    }

    modifier onlyOwnerOf(string memory _foodId) {
        require(foods[_foodId].exists, "Food not found");
        require(foods[_foodId].currentOwner == msg.sender, "Only current owner can operate");
        _;
    }

    function assignRole(address _account, DataModel.Role _role) public {
        require(msg.sender == admin, "Only admin can assign roles");
        roles[_account] = _role;
        emit RoleAssigned(_account, _role);
    }

    function createFood(string memory _foodId, string memory _name, string memory _origin) public onlyRole(DataModel.Role.Supplier) {
        require(!foods[_foodId].exists, "Food ID already exists");
        
        DataModel.Food storage newFood = foods[_foodId];
        newFood.foodId = _foodId;
        newFood.name = _name;
        newFood.origin = _origin;
        newFood.currentOwner = msg.sender;
        newFood.status = DataModel.FoodStatus.Created;
        newFood.exists = true;

        newFood.history.push(DataModel.TraceRecord({
            operator: msg.sender,
            role: DataModel.Role.Supplier,
            action: "Production",
            location: _origin,
            remark: "Food created by supplier",
            timestamp: block.timestamp
        }));

        foodIds.push(_foodId);
        emit FoodCreated(_foodId, msg.sender);
    }

    function transferToDistributor(string memory _foodId, address _distributor, string memory _location) public 
        onlyOwnerOf(_foodId) 
        onlyRole(DataModel.Role.Supplier) 
    {
        require(foods[_foodId].status == DataModel.FoodStatus.Created, "Invalid status for transfer");
        require(roles[_distributor] == DataModel.Role.Distributor, "Target must be a distributor");
        
        DataModel.Food storage food = foods[_foodId];
        food.currentOwner = _distributor;
        food.status = DataModel.FoodStatus.InTransit;

        food.history.push(DataModel.TraceRecord({
            operator: msg.sender,
            role: DataModel.Role.Supplier,
            action: "Shipped to Distributor",
            location: _location,
            remark: "In transit",
            timestamp: block.timestamp
        }));
    }

    function transferToRetailer(string memory _foodId, address _retailer, string memory _location) public 
        onlyOwnerOf(_foodId) 
        onlyRole(DataModel.Role.Distributor) 
    {
        require(foods[_foodId].status == DataModel.FoodStatus.InTransit, "Food must be in transit");
        require(roles[_retailer] == DataModel.Role.Retailer, "Target must be a retailer");

        DataModel.Food storage food = foods[_foodId];
        food.currentOwner = _retailer;
        food.status = DataModel.FoodStatus.Delivered;

        food.history.push(DataModel.TraceRecord({
            operator: msg.sender,
            role: DataModel.Role.Distributor,
            action: "Delivered to Retailer",
            location: _location,
            remark: "Delivery complete",
            timestamp: block.timestamp
        }));
    }

    function putOnSale(string memory _foodId, string memory _location) public 
        onlyOwnerOf(_foodId) 
        onlyRole(DataModel.Role.Retailer) 
    {
        require(foods[_foodId].status == DataModel.FoodStatus.Delivered, "Food not delivered yet");

        DataModel.Food storage food = foods[_foodId];
        food.status = DataModel.FoodStatus.OnSale;

        food.history.push(DataModel.TraceRecord({
            operator: msg.sender,
            role: DataModel.Role.Retailer,
            action: "Put on Sale",
            location: _location,
            remark: "Ready for consumers",
            timestamp: block.timestamp
        }));
    }

    // 开启 ABIEncoderV2 后，这里可以直接返回字符串数组了
    function getFoodHistory(string memory _foodId) public view returns (
        address[] memory operators,
        uint8[] memory roleList,
        string[] memory actions,
        string[] memory locations,
        uint256[] memory timestamps
    ) {
        require(foods[_foodId].exists, "Food not found");
        DataModel.TraceRecord[] storage history = foods[_foodId].history;
        
        operators = new address[](history.length);
        roleList = new uint8[](history.length);
        actions = new string[](history.length);
        locations = new string[](history.length);
        timestamps = new uint256[](history.length);

        for (uint256 i = 0; i < history.length; i++) {
            operators[i] = history[i].operator;
            roleList[i] = uint8(history[i].role);
            actions[i] = history[i].action;
            locations[i] = history[i].location;
            timestamps[i] = history[i].timestamp;
        }
    }

    function getFoodDetails(string memory _foodId) public view returns (
        string memory name,
        string memory origin,
        address currentOwner,
        uint8 status
    ) {
        require(foods[_foodId].exists, "Food not found");
        DataModel.Food storage f = foods[_foodId];
        return (f.name, f.origin, f.currentOwner, uint8(f.status));
    }

    function getFoodIds() public view returns (string[] memory) {
        return foodIds;
    }
}