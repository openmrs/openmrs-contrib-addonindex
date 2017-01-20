import React from "react";
import {render} from "react-dom";
import AllAddOns from "./component/AllAddOns";

render(
        <div>
            <h1>OpenMRS Add-On Index</h1>
            <AllAddOns></AllAddOns>
        </div>,
        document.getElementById('root')
)