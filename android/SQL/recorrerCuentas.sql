insert into Totales 
(_id, Cuenta, CantidadInicial, CurrentCantidad, IdMoneda, Activa, Tipo)
values
(20, "Prueba", 0, 0, 1, 0, 1)

insert into Totales (Cuenta, CantidadInicial, CurrentCantidad, IdMoneda, Activa, Tipo)
select Cuenta, CantidadInicial, CurrentCantidad, IdMoneda, Activa, Tipo from Totales where _id <> 20

update Movimiento set IdTotales = IdTotales + 20
update Movimiento set Traspaso = Traspaso + 20

delete from Totales where _id <= 20