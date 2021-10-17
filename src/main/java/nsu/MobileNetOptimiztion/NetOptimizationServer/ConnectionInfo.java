package nsu.MobileNetOptimiztion.NetOptimizationServer;

import nsu.MobileNetOptimiztion.NetOptimizationServer.Controller.CellInfo;
import nsu.MobileNetOptimiztion.NetOptimizationServer.Controller.Coords;

public class ConnectionInfo{
    public Coords getCoords() {
        return coords;
    }

    public void setCoords(Coords coords) {
        this.coords = coords;
    }

    public CellInfo getCell_info() {
        return cell_info;
    }

    public void setCell_info(CellInfo cellInfo) {
        this.cell_info = cellInfo;
    }

    Coords coords;
    CellInfo cell_info;
}