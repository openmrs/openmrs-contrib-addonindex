import { IconName } from "@fortawesome/fontawesome-common-types";

interface AddOnCollection {
  uid: string;
  name: string;
  description?: string;
  route: string;
  addOns?: AddOnCollectionItem[];
}

interface AddOnCollectionItem {
  uid: string;
  details: IAddOn;
  version: string;
}

interface IAddOn {
  uid: string;
  name: string;
  type: "OWA" | "OMOD";
  maintainers?: {
    name: string;
    url?: string;
  }[];
  status?: string;
  icon?: IconName | string;
  description?: string;
  tags?: string[];
  versions?: IAddOnVersion[];
  hostedUrl: string;
  links?: Link[];
  downloadCountInLast30Days?: number;
}

interface IAddOnVersion {
  version: string;
  downloadUri: string;
  releaseDatetime?: Date;
  requireOpenmrsVersion: string;
  renameTo?: string;
}

interface Link {
  title?: string;
  href: string;
  rel?: string;
}
