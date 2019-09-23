const TABLE_TEMPLATE =`
<hr/>
<!--<p><b>1:</b>
    <code>53073: select NON EMPTY {Hierarchize({{[Measures].[Surface du logement (moyenne)], [Measures].[Consomattion chauffage annuelle (min)], [Measures].[Consomattion chauffage annuelle (max)]}})} ON COLUMNS, NON EMPTY {Hierarchize({[Type d'activite du referent.REF_TYPACT_Hierarchie_1].[Type d'actvite].Members})} ON ROWS from [Cube4Chauffage]</code>
</p>-->
<div>
    <b class="mr-3"></b>
    <div class="btn-group btn-group-toggle" data-toggle="buttons">
        <label class="btn btn-outline-primary active"><input id="option1" type="radio"
                autocomplete="off" checked>Data</label>
        <label class="btn btn-outline-primary"><input id="option2" type="radio"
                autocomplete="off">Significance</label>
        <label class="btn btn-outline-primary"><input id="option3" type="radio"
                autocomplete="off">Surprise</label>
    </div>
    <!--<div class="dropdown float-right ml-2">
        <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton"
            data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
            Model Component
        </button>
        <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
            <a class="dropdown-item" href="#">1</a>
            <a class="dropdown-item" href="#">2</a>
            <a class="dropdown-item" href="#">3</a>
        </div>
    </div>
    <div class="btn-group btn-group-toggle float-right" role="group" data-toggle="buttons">
        <label class="btn btn-primary"><input name="options" id="all" type="radio"
                autocomplete="off">All</label>
        <label class="btn btn-primary"><input name="options" id="option1" type="radio"
                autocomplete="off">1</label>
        <label class="btn btn-primary"><input name="options" id="option2" type="radio"
                autocomplete="off">2</label>
        <label class="btn btn-primary"><input name="options" id="option3" type="radio"
                autocomplete="off">3</label>
    </div>-->
</div>
<table class="table table-sm table-bordered my-3">
    <tbody>
        <!--<tr>
            <th class="bg-light" scope="col" colspan="1" rowspan="1">#</th>
            <th class="bg-light" scope="col" colspan="1">Consomattion chauffage annuelle (max)</th>
            <th class="bg-light" scope="col" colspan="1">Consomattion chauffage annuelle (min)</th>
            <th class="bg-light" scope="col" colspan="1">Surface du logement (moyenne)</th>
        </tr>
        <tr>
            <th class="bg-light" scope="row" rowspan="1">Actifs ayant un emploi, y compris sous
                apprentissage ou en stage rémunéré</th>
            <td bgcolor="#cc4f4f">190004.0</td>
            <td bgcolor="#7489cc">126.0</td>
            <td bgcolor="#ccc785">101.039</td>
        </tr>
        <tr>
            <th class="bg-light" scope="row" rowspan="1">Chômeurs</th>
            <td bgcolor="#4f76cc">77856.0</td>
            <td bgcolor="#7489cc">69.0</td>
            <td bgcolor="#ccc785">77.497</td>
        </tr>
        <tr>
            <th class="bg-light" scope="row" rowspan="1">Retraités ou préretraités</th>
            <td bgcolor="#cc4f4f">190004.0</td>
            <td bgcolor="#cc2929">841.0</td>
            <td bgcolor="#ccc785">94.177</td>
        </tr>
        <tr>
            <th class="bg-light" scope="row" rowspan="1">Elèves, étudiants, stagiaires non rémunérés de 14
                ans ou plus</th>
            <td bgcolor="#292fcc">69599.0</td>
            <td bgcolor="#cc9829">708.0</td>
            <td bgcolor="#292fcc">40.9</td>
        </tr>
        <tr>
            <th class="bg-light" scope="row" rowspan="1">Femmes ou hommes au foyer</th>
            <td bgcolor="#2965cc">79571.0</td>
            <td bgcolor="#7489cc">172.0</td>
            <td bgcolor="#ccc785">86.997</td>
        </tr>
        <tr>
            <th class="bg-light" scope="row" rowspan="1">Autres inactifs</th>
            <td bgcolor="#4f76cc">77856.0</td>
            <td bgcolor="#7489cc">69.0</td>
            <td bgcolor="#ccc785">77.76</td>
        </tr>
        <tr>
            <th class="bg-light" scope="row" rowspan="1">Hors résidence principale</th>
            <td>nan</td>
            <td>nan</td>
            <td bgcolor="#ccc785">84.141</td>
        </tr>-->
    </tbody>
</table>
`