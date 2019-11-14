#Cube Explorer
This prototype is an atempt at implementing an intentional OLAP operator,
first described in (this paper)[https://www.sciencedirect.com/science/article/abs/pii/S0306437918303016?via%3Dihub] by PanosVassiliadis, PatrickMarcel and StefanoRizzi.

This EXPLAIN action is constrained by a time budget specified by the user and a starting view of the OLAP cube.

The objective is then to generate the most interesting exploration to "explain" the data in the original view.
This is done through operation inspired by analyst behaviors in browsing through cubes and simple statistics on the queries results

### Note : For the DOLAP experiments see branch 'dolap'.
