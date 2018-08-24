import {configure} from 'enzyme';
import Adapter from 'enzyme-adapter-react-15';
import jfm from 'jest-fetch-mock';

configure({adapter: new Adapter()});

global.fetch = jfm;