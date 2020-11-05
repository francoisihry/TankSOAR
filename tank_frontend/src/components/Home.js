import React, {Component} from 'react';
import Layout from "./Layout";
import CollapsibleSides from "./collapsibleSides";

const leftContent = <div>
    <div className='header'>
        <h3 className='title'>
            Left header
        </h3>
    </div>
    <div className='content'>
        <h3>Left content</h3>
        <p>
            Aenean ut felis finibus, aliquet mi a, feugiat felis. Donec porta, odio et vulputate
            laoreet, nibh odio iaculis mi, et ornare nulla orci vitae ligula. Sed mi velit, aliquam sit
            amet efficitur eget, scelerisque vel ligula. Aliquam finibus erat nec accumsan posuere.
            Vestibulum rhoncus, velit vitae volutpat vehicula, leo orci faucibus eros, at ornare nibh
            nunc nec mi. Donec porttitor ultricies mauris quis euismod. Praesent sem libero, venenatis
            ut ornare eget, volutpat tincidunt lacus. Pellentesque aliquam turpis et mauris consectetur,
            quis condimentum nunc dignissim. Cras lectus libero, pellentesque non malesuada at,
            condimentum nec ex. Nam sed accumsan enim. Donec eros massa, malesuada quis nulla elementum,
            imperdiet condimentum orci. Integer non velit et nulla vestibulum vestibulum. Proin vehicula
            tristique libero, eu tincidunt erat cursus ac. Ut malesuada ante ut est dictum, ornare
            varius arcu aliquet. Quisque vitae libero eget orci tristique aliquam id sit amet nunc.
        </p>
    </div>
</div>

const mainContent = <div>
    <div className='header'>
        <h3>
            Main header
        </h3>
    </div>
    <div className='content'>
        <h3>Main content</h3><br/>
        <p>
            Nam accumsan eleifend metus at imperdiet. Mauris pellentesque ipsum nisi, et fringilla leo
            blandit sed. In tempor, leo sit amet fringilla imperdiet, ipsum enim sagittis sem, non molestie
            nisi purus consequat sapien. Proin at velit id elit tincidunt iaculis ac ac libero. Vivamus
            vitae tincidunt ex. Duis sit amet lacinia massa. Quisque lobortis tincidunt metus ut commodo.
            Sed euismod quam gravida condimentum commodo.
        </p><br/>
        <p>
            Vivamus tincidunt risus ut sapien tincidunt, ac fermentum libero dapibus. Duis accumsan enim ac
            magna tempor, vestibulum euismod nisl pharetra. Ut dictum lacus eu venenatis vestibulum.
            Vestibulum euismod at arcu ac blandit. Curabitur eu imperdiet magna. Duis bibendum efficitur
            diam, eget placerat nunc imperdiet eget. Morbi porta at leo sed porta. Nullam eleifend eleifend
            quam eget dictum.
        </p><br/>
        <p>
            Sed nulla erat, lacinia sit amet dui at, cursus blandit neque. In ultricies, dui a laoreet
            dignissim, risus mi cursus risus, at luctus sem arcu non tortor. In hac habitasse platea
            dictumst. Etiam ut vulputate augue. Aenean efficitur commodo ipsum, in aliquet arcu blandit non.
            Praesent sed tempus dui, non eleifend nisi. Proin non finibus diam, quis finibus ante. Fusce
            aliquam faucibus mauris, id consequat velit ultricies at. Aliquam neque erat, fermentum non
            aliquam id, mattis nec justo. Nullam eget suscipit lectus.
        </p>
    </div>
</div>

const rightContent = <div>
    <div className='header'>
        <h3 className='title'>
            Right header
        </h3>
    </div>
    <div className='content'>
        <h3>Right content</h3><br/>
        <p>
            Mauris velit turpis, scelerisque at velit sed, porta varius tellus. Donec mollis faucibus
            arcu id luctus. Etiam sit amet sem orci. Integer accumsan enim id sem aliquam sollicitudin.
            Etiam sit amet lorem risus. Aliquam pellentesque vestibulum hendrerit. Pellentesque dui
            mauris, volutpat vel sapien vitae, iaculis venenatis odio. Donec vel metus et purus
            ullamcorper consequat. Mauris at ullamcorper quam, sed vehicula felis. Vestibulum fringilla,
            lacus sit amet finibus imperdiet, tellus massa pretium urna, non lacinia dui nibh ut enim.
            Nullam vestibulum bibendum purus convallis vehicula. Morbi tempor a ipsum mattis
            pellentesque. Nulla non libero vel enim accumsan luctus.
        </p>
    </div>
</div>

class Home extends Component {
    render() {
        return (
            <Layout>

                {/*<CollapsibleSides*/}
                {/*    leftContent={leftContent}*/}
                {/*    mainContent={mainContent}*/}
                {/*    rightContent={rightContent}*/}
                {/*/>*/}

                <h1>Home page</h1>

                To Dos:
                <ul>
                    <li><s>faire un choix du date format dans les settings: utiliser redux pour lire le format et setter
                        la date directement dans BackednInterface</s>
                    </li>
                    <li><s>BUG : quand on essaie de supprimer un compte avec @</s></li>
                    <li><s>Apparition de bug avec changement de login en hook : apres login on va plus direct sur l'url
                        qui avait été rentrée</s></li>
                    <li>Faire un indicateur de qui est en train de taffer sur un runbook pour empécher un mec d'aller
                        dessus
                    </li>
                    <li><s>Utiliser emotion pour faire les styles de react-tab et mettre ça dans styles: faire gaffe ya une
                        dep sur tank-settings-users-table dans Scripts</s>
                    </li>
                    <li><s>Renommer Scripts en Runbooks</s></li>
                    <li><s>Faire en sorte que le bouton OK de création de runbook vérifie que le champs name a bien été
                        entré</s>
                    </li>
                    <li><s>Re ecrire le modal avec un provider</s></li>
                    <li><s>Impossible supprimer runbook qui a un </s></li>
                    <li><s>Ajouter un oeuil pour voir le contenu du runbook dans un modal</s></li>
                    <li><s>Allow to switch language in the playbook editor</s></li>
                    <li>Allow to show/hide settings pannel in editor</li>
                    <li><s>Bug quand on supprime des users (voir si pareil avec les scripts)</s></li>
                    <li>Gérer les roles utilisateurs (affichage dans l'ui, restriction etc....)</li>
                    <li><s>Quitter le modal quand on appuit echap</s></li>
                    <li>Faire un home avec Dashboards</li>
                    <li>End2end tests avec Selenium</li>

                    <li><s>Mettre la fleche du select en bleu</s></li>
                    <li><s>Valider form login avec Enter</s></li>
                    <li>Faire playbook avec react flow chart qui génère le code python: un truc qui ressemble aux shemats de CIB</li>
                    <li><s>Enlever layoutSetting du backend et de redux</s> </li>
                    <li><s>Msg erreur quand ça se connecte pas au backend</s></li>
                </ul>

            </Layout>
        );
    }
}

export default Home;