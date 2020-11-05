import {mainBackgroundColorBlue, mainColorBeige, mainColorBlue} from "./index";

export const SelectStyles = {
    option: (provided, state) => ({
        ...provided,
        borderBottom: `1px solid ${mainColorBeige}`,
        fontWeight: state.isSelected ? 'bold' : state.isFocused ? 'bolder' : 'lighter',
        color: state.isSelected ? mainColorBeige:state.isFocused ? mainColorBlue : mainColorBeige,
        // padding: 20,
        backgroundColor: state.isSelected ? mainBackgroundColorBlue : state.isFocused ? mainColorBeige : mainBackgroundColorBlue,
    }),
    valueContainer: (provided, state) => ({
        ...provided,
        backgroundColor: mainBackgroundColorBlue,
        border: 'none',
        borderWidth: '0',
    }),
    singleValue: (provided, state) => {
        const opacity = state.isDisabled ? 0.5 : 1;
        const transition = 'opacity 300ms';

        return {
            ...provided, opacity, transition,
            color: mainColorBeige,
            textAlign: 'center',
            width: '100%',
        };
    },
    control: (provided, state) => ({
        ...provided,
        border: 'none',
        borderWidth: '0',
        backgroundColor: mainBackgroundColorBlue,
    }),
    dropdownIndicator: (provided, state) => ({
        ...provided,
        color: mainColorBlue,
        backgroundColor: mainBackgroundColorBlue,
    }),
    input: (provided, state) => ({
        ...provided,
        color: mainColorBeige,
        textAlign: 'center',
        width: '100%',
    }),
    menu: (provided, state) => ({
        ...provided,
        backgroundColor: mainBackgroundColorBlue,
    }),
    indicatorSeparator: (provided, state) => ({
        ...provided,
        backgroundColor: mainBackgroundColorBlue,
    }),
}